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
(require '[babashka.tasks :refer [shell]])
(import 'java.time.format.DateTimeFormatter
        'java.time.LocalDateTime)

(defn long-str
  "Join all string arguments together."
  [& strings]
  (string/join "\n" strings))

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

(def cli-options
  "CLI options for print the USAGE."
  [["-t"
    "--title TITLE"
    "New Post Title"
    :default "undefined"
    :validate [#(= (type %) java.lang.String) "Must be a string"]]
   ["-e"
    "--ext EXTENSION"
    "File extension"
    :default ".md"
    :validate [#(clojure.string/starts-with? % ".")
               "Must be a file extension starting with `.`"]]
   ["-h"
    "--help"
    "Display the help message"]])

(defn usage [options-summary]
  (->> ["Cljk - Jekyll manager in Clojure."
        ""
        "Usage: cljk.clj action [options]"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  serve    Run server with Jekyll"
        "  new      Create a new post with title -t, according to the following format:"
        "           YEAR-MONTH-DAY-title.EXTENSION"
        ""
        "See https://github.com/elias94/cljk for more information."]
       (string/join \newline)))

(defn error-msg
  "Format error message."
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  []
  (let [{:keys [options arguments summary errors]} (parse-opts *command-line-args* cli-options)]
    (cond
      (true? (:help options))
      {:exit-message (usage summary) :ok? true}
      errors
      {:exit-message (error-msg errors)}
      (and (= 1 (count arguments))
           (#{"serve" "new"} (first arguments)))
      {:action (first arguments) :options options})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;; Main
(let [{:keys [action options exit-message ok?]} (validate-args)]
  (cond exit-message
        (exit (if ok? 0 1) exit-message)
        (nil? action)
        (exit 1 "ERROR: No action provided. Use -h to print the usage.")
        :else
        (case action
          "serve" @(shell "bundle exec jekyll serve --host=0.0.0.0 --trace")
          "new"   (create-post options)
          nil)))
