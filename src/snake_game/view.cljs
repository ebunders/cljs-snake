(ns snake-game.view
  (:require
            [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
            ))

(defn render-board []
  (let [board (subscribe [:board])
        snake (subscribe [:snake])
        point (subscribe [:point])]
    (fn []
      (let [[width, height] @board
            snake-positions (into #{} @snake)
            current-point @point
            cells (for [y (range height)]
                    (into [:tr]
                          (for [x (range width)
                                :let [current-pos [x y]]]
                            (cond
                              (snake-positions current-pos) [:td.snake-on-cell]
                              (= current-pos current-point) [:td.point]
                              :else [:td.cell]))))]

        (into [:table.stage {:style {:height 377
                                     :width 527}}]
              cells)))))

(defn render-score
  "Render the player's score"
  []
  (let [points (subscribe [:points])]
     (fn []
       [:div.score (str "Score: " @points)])))


(defn render-state
  "Render the status of the game"
  []
  (let [game-state (subscribe [:game-running?])]
    [:div.status (str "Game: " (if game-state "Running" "Over"))]))


(defn render-pause
  "Render the paused/not paused state of the game"
  []
  (let [paused (subscribe [:game-paused?])]
    (fn []
      (if @paused
             [:div.overlay
              [:div.play
               [:h2 "Pause.."]]]
             [:div]))))


(defn render-game-over
  "Render the game"
  []
  (let [game-state (subscribe [:game-running?])]
    (fn [] (if @game-state
             [:div]
             [:div.overlay
              [:div.play {:on-click #(dispatch [:initialize])}
               [:h1 "↺"]]]))))

