(ns snake-game.save-state
  (:require [re-frame.core :refer [reg-event-db subscribe dispatch]]))

(reg-event-db
  :load-state
  (fn [db [_ state]]
    (let [saves (:saves db)]
      (assoc state :saves saves))))

(reg-event-db
  :save-state
  (fn [db _]
    (update db :saves #(conj % ["save" (dissoc db :saves)]))))


(defn render-saves
  "Render the list of saved states and the 'save' button"
  []
  (let [saves (subscribe [:saves])]
    (fn []
      (.log js/console (str "saves is: " @saves))
      [:div
       [:h3 "Saves:"]
       (into [:ul] (map (fn [save]
                          [:li
                           [:a {:on-click #(dispatch [:load-state (second save)])}(first save)]
                           ]) @saves))
       [:button {:on-click #(dispatch [:save-state])} "Save state"]

       ])))