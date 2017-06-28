(ns engine.game
  (:require [re-frame.core :refer [reg-event-fx reg-event-db dispatch-sync dispatch]]
            [goog.events :as events]))
; The game allows you to register handlers for key- game-state commbinations..
; - Use reg-key-bindings to register events for key and game-state combinations
; - Use update-game-state to set the current game state directly
; - Use the :change-game-state event to set the new gamestate through events.


(comment {:key-handlers {:running [[37 :sn-up]
                                   [38 :sn-right]
                                   [32 :sn-pause]]
                          :loaded [[37 :menu-up]
                                    [:39 :menu-down]]}})

(enable-console-print!)
(defn log [& strs]
  (.log js/console (reduce str (interleave strs (repeat " ")))))

;
; GAME CONFIG
;
(defonce game-config
         (atom {:key-handlers {}
                        :game-state   :loaded}))

(defn- update-game-config-keybindings [game-config game-state key-code event]
  (update-in game-config [:key-handlers game-state] #(into [] (conj % [key-code event]))))


(defn reg-key-bindings
  [game-state key-code event]
  (swap! game-config #(update-game-config-keybindings % game-state key-code event))
  (log "registring key binding: " game-state key-code event))



;
; Handle game state changes
;
(defn update-game-state [new-state] (swap! game-config #(assoc %1 :game-state new-state)))

(reg-event-db :change-game-state (fn [db [_ new-state]]
                                   (update-game-state new-state)
                                   db))


;
; Handling the key events
;
(defn- handle-key-event [key-code state-handlers]
  "Invokes the handlers for this keycode and this state
  Returns true if some handler was executed for this key in this game state"
  (if-let [handlers-for-key (seq (filter #(= key-code (first %1)) state-handlers))]
    (do
            (doseq [[_ event] handlers-for-key] (dispatch [event]))
            true)
    false)

  )


(defonce key-handler
         (events/listen js/window "keydown"
                        (fn [e]
                          (let [key-code (.-keyCode e)
                                game-state (:game-state @game-config)
                                state-handlers (-> @game-config :key-handlers game-state)]
                            (if (and
                                  state-handlers
                                  (handle-key-event key-code state-handlers))
                              (.preventDefault e))))))




