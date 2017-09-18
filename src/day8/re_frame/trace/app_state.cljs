(ns day8.re-frame.trace.app-state
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.formatters.core :as cljs-devtools]

            [cljs.pprint :refer [pprint]]))

(defn css-munge
  [string]
  (str/replace string #"\.|/" "-"))

(defn namespace-css
  [classname]
  (str "re-frame-trace--" classname))

(defn type-string
  [obj]
  (cond
    (number? obj)    "number"
    (boolean? obj)   "boolean"
    (string? obj)    "string"
    (nil? obj)       "nil"
    (keyword? obj)   "keyword"
    (symbol? obj)    "symbol"
    :else (pr-str (type obj))))

(defn view
  [data]
  (if (coll? data)
    [:div  {:class (str (namespace-css "collection") " " (namespace-css (css-munge (type-string data))))}]
    [:span {:class (str (namespace-css "primative") " " (namespace-css (css-munge (type-string data))))} (str data)]))

(defn string->css [css-string]
  (->> (map #(str/split % #":") (str/split (get css-string "style") #";"))
       (reduce (fn [acc [property value]]
                 (assoc acc (keyword property) value)) {})))

(defn str->hiccup
  [string]
  (cond (= string "span")   :span
        (= string "style")  :style
        (= string ", ")     " "
        :else               string))

(defn crawl
  [data]
  (if (coll? data)
    (into (view data) (mapv crawl data))
    (view data)))

(defn jsonml->hiccup
  [jsonml]
  (cond
    (array? jsonml)    (if (= "object" (get jsonml 0))
                         [:span.re-frame-trace--object
                           (jsonml->hiccup (cljs-devtools/header-api-call
                                             (.-object (get jsonml 1))
                                             (.-config (get jsonml 1))))]
                         (mapv jsonml->hiccup jsonml))
    (object? jsonml)   {:style (string->css (js->clj jsonml))}
    :else              (str->hiccup jsonml)))

(defn tab [data]
  [:div {:style {:flex "1 0 auto" :width "100%" :height "100%" :display "flex" :flex-direction "column"}}
    [:div.panel-content-scrollable
      (jsonml->hiccup (cljs-devtools/header-api-call data))]])
