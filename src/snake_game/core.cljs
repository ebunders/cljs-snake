(ns snake-game.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [goog.events :as events]
            [snake-game.view :as view]
            [snake-game.logic :as logic]))

(enable-console-print!)

(println "This text is printed from src/snake-game/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

;(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
(def board [35 25])
(def snake {:direction [1 0]
            :body [[3 2] [2 2] [1 2] [0 2]]})




(def initial-state {
                    :board board
                    :snake snake
                    :point (logic/random-free-position snake board)
                    :points 0
                    :game-running? true})


;;
;; HANDLERS
;;


(register-handler
 :initialize
 (fn [db _]
   (merge db initial-state)))

(register-handler
  :next-state
  (fn [db _]
    (if (:game-running? db)
          ;;(assoc db :snake (logic/move-snake (:snake db)))
          (-> db
              (update-in [:snake] logic/move-snake)
              (as-> after-move
                    (logic/process-move after-move)))
          db)))


(register-handler
  :change-direction
  (fn [db [_ new-direction]]
    (if (:game-running? db)
      (update-in db
                 [:snake :direction]
                 (partial logic/change-snake-direction new-direction)))))


(defn game
  "the main rendering funciton"
  []
  [:div#foo
   [view/render-board]
   [view/render-score]
   [view/render-game-over]])


(defn run
  "The main app function"
  []
  (dispatch-sync [:initialize])
  (reagent/render [game]
                  (.getElementById js/document "app")))

(defonce snake-moving
         (js/setInterval #(dispatch [:next-state]) 150))

(defonce key-handler
         (events/listen js/window "keydown"
                        (fn [e]
                          (let [key-code (.-keyCode e)]
                            (if (contains? logic/key-code->move key-code)
                              (dispatch [:change-direction (logic/key-code->move key-code)]))))))

;;
;; SUBSCRIPTIONS
;;


(register-sub
 :board
 (fn
   [db _]
   (reaction (:board @db))))

(register-sub
 :snake
 (fn [db _]
   (reaction (:body (:snake @db)))))

(register-sub
 :point
 (fn [db _]
   (reaction (:point @db))))

(register-sub
  :points
  (fn [db _]
    (reaction (:points @db))))

(register-sub
  :game-running?
  (fn [db _]
    (reaction (:game-running? @db))))


 (run)
