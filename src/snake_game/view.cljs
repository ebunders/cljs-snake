(ns snake-game.view
  (:require
    [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
    ))

(defn render-snake [current-pos snake]
  (let [snake-head (first (:body snake))
        at-snake-head? (= current-pos snake-head)
        blinking? (:blink-head snake)]

    (if (and blinking? at-snake-head?)
      (do
        (.log js/console "current pos: " (str current-pos) " snake head:" (str snake-head) " blinking: " (str blinking?))
        [:div.cell.snake-on.blink])
      [:div.cell.snake-on])))

(defn render-point [] [:div.cell.point "*"])

(defn render-cell [] [:div.cell])


(defn render-board []
  (let [board (subscribe [:board])
        snake (subscribe [:snake])
        point (subscribe [:point])]
    (fn []
      (let [[width, height] @board
            snake-positions (into #{} (:body @snake))

            current-point @point
            cells (for [y (range height)]
                    (into [:div.gamerow]
                          (for [x (range width)
                                :let [current-pos [x y]]]
                            (cond
                              (snake-positions current-pos) (render-snake current-pos @snake)
                              (= current-pos current-point) (render-point)
                              :else (render-cell)))))]

        (into [:div.stage]
              cells)))))

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

(defn render-score
  "Render the player's score"
  []
  (let [points (subscribe [:points])]
    (fn []
      [:div.score (str "Score: " @points)])))


(defn render-state
  "Render the status of the game"
  []
  (let [game-state (subscribe [:game-state])]
    [:div.status (str "Game: " @game-state)]))


(defn render-pause
  "Render the paused/not paused state of the game"
  []

  (fn []
    (let [game-state (subscribe [:game-state])
          game-paused? (= @game-state :game-paused)]
      (if game-paused?
        [:div
         [:div.play
          [:h2 "Pause.."]]]
        [:div]))))


(defn render-game-over
  "Render the game"
  []
  (fn []
    (let [game-state (subscribe [:game-state])
          finished? (= :game-finished @game-state)]
      (if finished?
        [:div.overlay
         [:div.play {:on-click #(dispatch [:initialize])}
          [:h1 "↺"]]]
        [:div]))))

