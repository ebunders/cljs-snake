(ns engine.game
  (:require [re-frame.core :refer [reg-event-fx reg-event-db dispatch-sync dispatch]]
            [goog.events :as events]))
; The game allows you to register handlers for different keys.
; those handlers will only be called when the game is not paused.
; the client can provide a key for pause.
; When the game is paused, it will only respond to the pause key.
;
; the client can register a game tick function. This function is
; called every nth second, but only if the game is not paused.


;
; GAME CONFIG
;
(def game-config (atom {:key-handlers {}
                        :game-state   :loaded}))
(def pause-key 32)

(defn update-game-config-keybindings [game-config game-state pred f]
  (let [game-states [:loaded :game-running :paused :ended]]
    (if (some #{game-state} game-states)
      (update-in game-config [:key-handlers game-state] #(into [] (cons [pred f] %)))
      game-config)))

(defn reg-key-bindings!
  "[game-state pred f] Register handlers for certain key presses.
  game state is one of [:loaded :running :paused :ended]
  pred is a f that take the key nr as argument, and returns bolean
  f the function that is run when the pred returns true for the given key
  "
  [game-state pred f]
  (swap! game-config #(update-game-config-keybindings % game-state pred f)))


;
; INIT
;


(def initial-state
  {:__game_data {
                 :game-mode :loaded
                 :paused    false
                 }
   :game        nil})

(reg-event-db
  :initialize
  (fn [db _]
    (merge db initial-state)))


(defn run
  []
  (dispatch-sync [:initialize]))

;
; HANDLERS
;

;(defn re-event-gamedb
;  "Wrapper for re-event-db that "
;  [event handler]
;  (reg-event-fx event
;                (fn [cofx event]
;                  {
;                   :db (update-in cofx [:db :game] (handler (:game (:db cofx)) event))
;                   }))
;  )

(defn filter-and-invoke [value, list]
  "Accepts a list of predicate-consumer pairs, and calls (consumer value)
  where (predicate value) returns 'true'
  returns a list of the results of (consumer value)"
  (->> list
       (filter (fn [[pred _]] (pred value)))
       (map (fn [[_ consumer]] (consumer value)))))


(defn handle-key-event [key-code state-handlers]
    (filter-and-invoke key-code state-handlers)
    (some? (some true? (map (fn [[pred _]] (pred key-code)) state-handlers))))


(defonce key-handler
         (events/listen js/window "keydown"
                        (fn [e]
                          (let [key-code (.-keyCode e)
                                game-state (:game-state @game-config)
                                state-handlers (-> @game-config :key-handlers game-state)]
                            (if (= key-code pause-key)
                              (dispatch [:toggle-pause])
                              (if (handle-key-event key-code state-handlers)
                                (.preventDefault e)))))))




