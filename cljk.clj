#!/usr/bin/env bb

;;; Cljk --- Jekyll manager in Clojure
;;
;; Copyright (c) 2022 Elia Scotto
;; 
;; Author: Elia Scotto [tw: @elia_scotto]
;; Maintainer: Elia Scotto [tw: @elia_scotto]
;; URL: https://github.com/elias94/cljk
;; Keywords: i18n
;; Version: 1.0
;;
;; USAGE:
;; 0. install babashka 
;;    `bash < <(curl -s https://raw.githubusercontent.com/babashka/babashka/master/install)`
;; 1. add `cljk.clj` into your jekyll dir
;; 2. run `./cljk.clj` from that directory

(require '[clojure.tools.cli :refer [parse-opts]])
(require '[clojure.string :as string])
(import 'java.time.format.DateTimeFormatter
        'java.time.LocalDateTime)

(defn long-str [& strings] (string/join "\n" strings))

(def date
  "Current date-time."
  (LocalDateTime/now))

(def year-formatter
  "Year date formatter."
  (DateTimeFormatter/ofPattern "yyyy-MM-dd"))

(def full-date-formatter
  "Default format for date-time rappresentation
  on post filenames."
  (DateTimeFormatter/ofPattern "yyyy-MM-dd"))

(defn format-date
  "Return current date formatted using formatter."
  [formatter]
  (.format date formatter))

(defn new-post
  "Create new post initial content."
  [m]
  (let [format-map #(apply (fn [k v]
                             (str (name k) ": " v))
                           %)
        formatted-data (->> m
                            (seq)
                            (map format-map)
                            (string/join "\n"))]
    (long-str "---"
              "layout: post"
              formatted-data
              "---\n\n")))

(defn post-template
  "Default template for new post."
  [title]
  (new-post {:title (or title "Undefined")
             :date (format-date year-formatter)
             :categories "how"}))

(def cli-options
  "CLI options for usage."
  [["-t" "--title TITLE" "New Post Title"
    :default "undefined"
    :validate [#(= (type %) java.lang.String) "Must be a string"]]
   ["-e" "--ext EXTENSION" "File extension"
    :default ".md"
    :validate [#(clojure.string/starts-with? % ".")
               "Must be a file extension starting with `.`"]]
   ["-h" "--help" "Display the help message"]])

(defn post-filename
  "Create the new filename for the post."
  [title ext]
  (str
   "src/_posts/"
   (format-date full-date-formatter)
   "-"
   (string/lower-case (string/replace title #" " "-"))
   ext))

(defn create-post
  "Create new Jekyll markdown post file with title."
  [{title :title ext :ext}]
  (spit
   (post-filename title ext)
   (post-template title)))

;; main
(let [{:keys [options summary errors] _ :arguments}
      (parse-opts *command-line-args* cli-options)]
  (when errors
    (println (string/join "\n" errors))
    (System/exit 1))
  (if (true? (:help options))
    (println (long-str
              "Usage: cljk.clj -t \"Post Title\"\n"
              "Create a new post named according to the following format:"
              "YEAR-MONTH-DAY-title.EXTENSION"
              "\nFlags:"
              summary))
    (create-post options)))