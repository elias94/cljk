# Cljk - Jekyll manager in Clojure

Like npm but for Jekyll

- run `./cljk.clj serve` for run jekyll serve
- run `./cljk.clj new -t "post title"` for a new post

## Usage

0. install babashka 
   ```bash
   bash < <(curl -s https://raw.githubusercontent.com/babashka/babashka/master/install)
   ```
1. add `cljk.clj` into your jekyll dir
2. `chmod +x cljk.clj`
3. run `./cljk.clj` from that directory

## Options

```
-t, --title TITLE    undefined  New Post Title
-e, --ext EXTENSION  .md        File extension
-h, --help                      Display the help message
```
