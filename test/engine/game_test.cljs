;;https://8thlight.com/blog/eric-smith/2016/10/05/a-testable-clojurescript-setup.html
(ns engine.game-test
  (:require [engine.game :as p]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-game-udpate-game-config
  (let
    [game-config {:key-handlers {}}
     game-config-result {:key-handlers {:loaded [[true true]]}}

     game-config-result2 {:key-handlers {:loaded [[true true]
                                                [true true]]}}

     game-config-result3 {:key-handlers {:loaded       [[true true]
                                                   [true true]]
                                         :game-running [[true true]]}}
     ]
    (testing "The key-event api"
      (testing "should allow registration of key eventlisteners"
        (let [result (p/update-game-config-keybindings game-config :loaded true true)]
          (is (= 1 (count (-> result :key-handlers :loaded))))
          (is (= game-config-result result) "A handler should be added to the :loaded phase"))

        (let [result (p/update-game-config-keybindings game-config-result :loaded true true)]
          (is (= 2 (count (-> result :key-handlers :loaded))) "A second handler should have been added to :loaded")
          (is (= result game-config-result2)))

        (let [result (p/update-game-config-keybindings game-config-result2 :game-running true true)]
          (is (= 2 (count (-> result :key-handlers :loaded))) "Two handlers for :loaded")
          (is (= 1 (count (-> result :key-handlers :game-running))) "One handler for :running")

          (is (= result game-config-result3)  "A handler should be added to :running" )))

      (testing "Should be able to filter and invoke key handlers"
        (let [l [[#(> % 0) inc] [#(> % 1) inc] [#(> % 2) inc]]]
          (is (= [] (p/filter-and-invoke 0 l)) "value 0 should have no handlers")
          (is (= [2] (p/filter-and-invoke 1 l)) "value 1 should have 1 handler")
          (is (= [3 3] (p/filter-and-invoke 2 l)) "value 2 should have 2 handlers")

          ))

      (testing "should be able to handle key events"
        (let  [state-handlers [[#(= % 1) #(%)]
                              [#(= % 2) #(%)]
                              [#(= % 2) #(%)]]]
          (is (= true  (p/handle-key-event 1 state-handlers )) "key-code 1 should trigger handler :foo")
          (is (= true  (p/handle-key-event 2 state-handlers )) "key-code 1 should trigger handler :foo")
          (is (= false  (p/handle-key-event 3 state-handlers )) "key-code 1 should trigger handler :foo")
          )))))
