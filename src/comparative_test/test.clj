(ns comparative-test.test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as string]
            [comparative-test.diff :as diff]))

(defn shell [& args]
  (println "--shelling--->:" (pr-str args))
  (let [{:keys [:exit :err :out]} (apply shell/sh args)]
    (when (not (zero? exit))
      (println ">> FAILED"))
    (println "exit:" exit)
    (when (not (string/blank? out)) (println "out:" out))
    (when (not (string/blank? err)) (println "err:" err))
    exit))

(defn shell-or-die [& args]
  (let [exit (apply shell args)]
    (or (zero? exit)
        (System/exit exit))))

(defn old-analyze [{:keys [project] :as args}]
  (println "old-analyze" (pr-str args))
  (spit (str "results/" (string/replace project #"/" "_") ".old-analyze.out")
        (with-out-str
          (shell "time"
                 "clojure"
                 "-m" "cljdoc.analysis.runner-ng"
                 (pr-str args)
                 :dir "./cljdoc/modules/analysis-runner"))))

(defn new-analyze [{:keys [project] :as args}]
  (println "new-analyze" (pr-str args))
  (spit (str "results/" (string/replace project #"/" "_") ".new-analyze.out")
        (with-out-str
          (shell "time"
                 "clojure"
                 "-m" "cljdoc-analyzer.cljdoc-main"
                 (pr-str args)
                 :dir "./cljdoc-analyzer"))))

(defn diff [{:keys [project]}]
  (let [f (str "results/" (string/replace project #"/" "_") ".diff")]
    (println "--diffing-->" project "to" f)
    (spit f (with-out-str (diff/diff project)))))

(def projects
  [{:project "amazonica"
    :version "0.3.146"
    :jarpath "http://repo.clojars.org/amazonica/amazonica/0.3.146/amazonica-0.3.146.jar"
    :pompath "http://repo.clojars.org/amazonica/amazonica/0.3.146/amazonica-0.3.146.pom"}
   {:project "bidi"
    :version "2.1.3"
    :jarpath "http://repo.clojars.org/bidi/bidi/2.1.3/bidi-2.1.3.jar"
    :pompath "http://repo.clojars.org/bidi/bidi/2.1.3/bidi-2.1.3.pom"}
   {:project "iced-nrepl"
    :version "0.2.5"
    :jarpath "http://repo.clojars.org/iced-nrepl/iced-nrepl/0.2.5/iced-nrepl-0.2.5.jar"
    :pompath "http://repo.clojars.org/iced-nrepl/iced-nrepl/0.2.5/iced-nrepl-0.2.5.pom"}
   {:project "io.aviso/pretty"
    :version "0.1.29"
    :jarpath "http://repo.clojars.org/io/aviso/pretty/0.1.29/pretty-0.1.29.jar"
    :pompath "http://repo.clojars.org/io/aviso/pretty/0.1.29/pretty-0.1.29.pom"}
   {:project "lilactown/hx"
    :version "0.5.2"
    :jarpath "http://repo.clojars.org/lilactown/hx/0.5.2/hx-0.5.2.jar"
    :pompath "http://repo.clojars.org/lilactown/hx/0.5.2/hx-0.5.2.pom"}
   {:project "lread/rewrite-cljs-playground"
    :version "1.0.0-alpha"
    :jarpath "/Users/lee/.m2/repository/lread/rewrite-cljs-playground/1.0.0-alpha/rewrite-cljs-playground-1.0.0-alpha.jar"
    :pompath "/Users/lee/.m2/repository/lread/rewrite-cljs-playground/1.0.0-alpha/rewrite-cljs-playground-1.0.0-alpha.pom"}
   {:project "manifold"
    :version "0.1.8"
    :jarpath "http://repo.clojars.org/manifold/manifold/0.1.8/manifold-0.1.8.jar"
    :pompath "http://repo.clojars.org/manifold/manifold/0.1.8/manifold-0.1.8.pom"}
   {:project "metosin/compojure-api"
    :version "2.0.0-alpha27"
    :jarpath "http://repo.clojars.org/metosin/compojure-api/2.0.0-alpha27/compojure-api-2.0.0-alpha27.jar"
    :pompath "http://repo.clojars.org/metosin/compojure-api/2.0.0-alpha27/compojure-api-2.0.0-alpha27.pom"}
   {:project "metosin/muuntaja"
    :version "0.6.3"
    :jarpath "http://repo.clojars.org/metosin/muuntaja/0.6.3/muuntaja-0.6.3.jar"
    :pompath "http://repo.clojars.org/metosin/muuntaja/0.6.3/muuntaja-0.6.3.pom"}
   {:project "metosin/reitit"
    :version "0.3.9"
    :jarpath "http://repo.clojars.org/metosin/reitit/0.3.9/reitit-0.3.9.jar"
    :pompath "http://repo.clojars.org/metosin/reitit/0.3.9/reitit-0.3.9.pom"}
   {:project "orchestra"
    :version "2018.11.07-1"
    :jarpath "http://repo.clojars.org/orchestra/orchestra/2018.11.07-1/orchestra-2018.11.07-1.jar"
    :pompath "http://repo.clojars.org/orchestra/orchestra/2018.11.07-1/orchestra-2018.11.07-1.pom"}
   {:project "semantic-csv"
    :version "0.2.1-alpha1"
    :jarpath "http://repo.clojars.org/semantic-csv/semantic-csv/0.2.1-alpha1/semantic-csv-0.2.1-alpha1.jar"
    :pompath "http://repo.clojars.org/semantic-csv/semantic-csv/0.2.1-alpha1/semantic-csv-0.2.1-alpha1.pom"}])

(defn -main []

  (println "comparative test for cljdoc-analyzer")
  (shell-or-die "rm" "-rf" "cljdoc")
  (shell-or-die "rm" "-rf" "cljdoc-analyzer")
  (shell-or-die "rm" "-rf" "rewrite-cljs-playground")

  (shell-or-die "git" "clone" "git@github.com:cljdoc/cljdoc.git")
  (shell-or-die "git" "clone" "git@github.com:lread/cljdoc-analyzer.git")
  (shell-or-die "git" "clone" "git@github.com:lread/rewrite-cljs-playground.git")

  ;; let's be consistent, choose shas as of this writing
  (shell-or-die "git" "reset" "--hard" "7f3985e139905e729803e2d22dc9491d5907e92c" :dir "cljdoc")
  (shell-or-die "git" "reset" "--hard" "f69a43b33c9727d31a066fb86564249b08d6da7a" :dir "cljdoc-analyzer")
  (shell-or-die "git" "reset" "--hard" "e94dc733749dcd2bf0beaf625130eca51eb6aba3" :dir "rewrite-cljs-playground")

  ;; not on clojars yet, install locally
  (shell-or-die "mvn" "install" :dir "rewrite-cljs-playground")

  (shell-or-die "rm" "-rf" "/tmp/cljdoc")
  (shell-or-die "rm" "-rf" "results")
  (shell-or-die "mkdir" "results")

  (run! #(old-analyze %) projects)
  (when (.exists (io/file "/tmp/cljdoc"))
    (shell-or-die "cp" "-r" "/tmp/cljdoc" "results/old"))

  (shell-or-die "rm" "-rf" "/tmp/cljdoc")
  (run! #(new-analyze  %) projects)
  (when (.exists (io/file "/tmp/cljdoc"))
    (shell-or-die "cp" "-r" "/tmp/cljdoc" "results/new"))

  (run! #(diff %) projects)

  (println "done")
  (System/exit 0))
