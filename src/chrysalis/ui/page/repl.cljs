;; Chrysalis -- Kaleidoscope Command Center
;; Copyright (C) 2017  Gergely Nagy <algernon@madhouse-project.org>
;;
;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns chrysalis.ui.page.repl
  (:require [reagent.core :as reagent]
            [chrysalis.ui.page :refer [pages page]]
            [chrysalis.utils :refer [state send-command!]]))

(defn repl-wrap [req index result]
  [:div.row {:key (str "repl-history-" index)
             :style {:margin-bottom "1em"}}
   [:div.col-sm-12
    [:div.card
     [:div.card-block
      [:span.card-title
       [:i.fa.fa-angle-down] " " [:code req]]
      (if-not (= result [:pre "\"\""])
        result
        [:pre [:i "<no output>"]])]]]])

(defmulti display
  (fn [command _ _ _]
    command))

(defmethod display :default [_ req result index]
  (when result
    (repl-wrap req index
               [:pre  (.stringify js/JSON (clj->js result) nil 2)])))

(defmethod page :repl [_]
  [:div.container-fluid
   [:div.row
    [:div.col-12.text-center
     [:h2 "REPL"]]]
   (doall (map (fn [item index]
                 (display (:command item) (:request item) @(:result item) index))
               (reverse (get-in @state [:repl :history])) (range)))
   [:div.row.justify-content-left {:style {:margin-bottom "1em"}}
    [:form.col-sm-12 {:style {:background-color "rgb(211,211,211)"
                              :padding-bottom "3px"}
                      :on-submit (fn [e]
                                   (.preventDefault e)
                                   (send-command! (get-in @state [:repl :command]))
                                   (swap! state assoc-in [:repl :command] nil))}
     [:div {:style {:margin-right "1em"
                    :display :inline-block
                    :vertical-align "-webkit-baseline-middle"}} [:i.fa.fa-angle-right]]
     [:input {:type :text
              :placeholder "Type command here"
              :autoFocus true
              :style {:border 0
                      :width "75%"
                      :vertical-align "-webkit-baseline-middle"
                      :padding-left "0.25em"
                      :padding-right "0.25em"
                      :outline :none}
              :value (get-in @state [:repl :command])
              :on-change (fn [e]
                           (swap! state assoc-in [:repl :command] (.-value (.-target e))))}]]]])

(swap! pages assoc :repl {:name "REPL"})
(swap! state assoc :repl {})