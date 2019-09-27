(ns comparative-test.diff
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.set :as cset]
            [clojure.walk :as walk]
            [lambdaisland.deep-diff :as ddiff]
            [cljdoc-analyzer.analysis-edn :as aedn]))

;; summary of differences
;; change                                                              first found in
;; ------                                                              --------------
;; :codox is now :analysis                                             amazonica
;; not consistently sorted
;; every public includes :members even when empty list
;; members contained a nil :doc
;; defrecord vars are now included example:
;;  {:file "muuntaja/core.clj", :line 41, :name Adapter, :type :var}   muuntaja,compojure-api,bidi, orchestra
;; members contained :file and :line (same values as in public)        bidi
;; fix for cljs cljc in same namespace (orchestra only showed cljc)    orchestra

;; manifold
;; - found bug in cljdoc-analyzer including manifold nses with no publics...
;;;   but maybe that is normal,, ns might have doc or something.
;; - some ns now included that were not before...
;;   manifold.debug
;;   manifold.stream.core
;;   manifold.stream.default
;;   manifold.stream.graph
;;   manifold.stream.deferred
;;   manifold.utils
;; ah... namespace override config is not working!


(defn- case-insensitive-comparator [a b]
  (let [la (and a (string/lower-case a))
        lb (and b (string/lower-case b))]
    (if (= la lb)
      (compare a b)
      (compare la lb))))

(defn- sort-by-name
  "Sorts all collections of maps with :name by :name.
  Sort is case insensitive with consistent sort order if :name is case sensitive unique across collection
  and all maps in collection have :name."
  [namespaces]
  (walk/postwalk #(if (and (coll? %) (:name (first %)))
                    (sort-by :name case-insensitive-comparator %)
                    %)
                 namespaces))

(defn- turf-empty
  [e k]
  (walk/postwalk #(if (and (map? %) (contains? % k) (or (nil? (k %)) (empty? (k %))))
                    (dissoc % k)
                    %)
                 e))

(defn- turf-members-file-line
  [e]
  (walk/postwalk #(if (and (map? %) (contains? % :members))
                    (update % :members
                            (fn [ms]
                              (map
                               (fn [m] (dissoc m :file :line))
                               ms)))
                    %)
                 e))

(defn load-edn [f]
  (let [e (aedn/read f)]
    (if (:codox e)
      (-> e
          (cset/rename-keys {:codox :analysis})
          (turf-empty :members)
          (turf-empty :doc)
          (turf-members-file-line)
          (sort-by-name))
      e)))

(defn- find-edn-file [proj-specifier test-set]
  (let [fs (->> (file-seq (io/file "results"))
                (filter #(re-matches #".*/cljdoc\.edn" (str %)))
                (filter #(re-find (re-pattern proj-specifier) (str %)))
                (filter #(re-find (re-pattern test-set) (str %))))]
    (if (= 1 (count fs))
      (println "found" (str (first fs)))
      (throw (ex-info (str  "expected 1 match for result file " proj-specifier " " test-set " got: " (pr-str fs)) {})))
    (first fs)))


(defn diff [proj-specifier]
  (try
    (let [ea (load-edn (find-edn-file proj-specifier "old"))
          eb (load-edn (find-edn-file proj-specifier "new"))]

      (if (= ea eb)
        (println "no differences found after compensations in old")
        (ddiff/pretty-print (ddiff/diff ea eb))))
    (catch Throwable e
      (println "ERROR during diff:")
      (println e))))

(defn -main [proj-specifier]
  (diff proj-specifier)
  (System/exit 0))
