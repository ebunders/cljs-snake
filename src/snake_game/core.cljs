(ns snake-game.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            [goog.events :as events]))

(enable-console-print!)

(println "This text is printed from src/snake-game/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
(def board [35 25])
(def snake {:direction [1 0]
            :body [[3 2] [2 2] [1 2] [0 2]]})

(defn random-free-position
  "This function takes the snake and the board-size as arguments,
  and returns a random position not colliding with the snake body"
  [snake [x y]]
  (let [snake-postions-set (into #{} (:body snake))
       board-positions (for [x-pos (range x)
                             y-pos (range y)]
                         [x-pos y-pos])]
  (when-let [free-positions (seq (remove snake-postions-set board-positions))]
    (rand-nth free-positions))))


(def initial-state {
                    :board board
                    :snake snake
                    :point (random-free-position snake board)
                    :points 0
                    :game-running? true})
