(ns clojure-dojo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! timeout]]
            [clojure.string :as string]))

(defonce app-state (atom {:text "Todo:"
                          :todos [{:text "implement todos" :done false }
                                  {:text "react to enter key" :done false}]}))


(defn todo-item-view [item owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [change-queue]}]
      (dom/li nil (:text item)
              (dom/input #js{:type "checkbox"
                             :checked (:done item)
                             :onClick (fn [e] (put! change-queue item))})))))


(defn add-item [app owner]
  (let [input (om/get-node owner "new-item")
        new-item (.-value input)]
    (when (and  new-item (not (string/blank? new-item)))
      (om/transact! app :todos #(conj % {:text new-item :done false}))
      (set! (.-value input) "")
      (.focus input))))

(defn update-item-state [item new-state]
  (fn [todos] (replace {item (assoc item :done new-state)} todos)))

(defn delete-item [app to-delete]
  (om/transact! app :todos
                (fn [todos] (vec (remove #(= to-delete %) todos)))))


(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IInitState
        (init-state [_]
          {:change-queue (chan)
           :delete-queue (chan 20)})
        om/IWillMount
        (will-mount [_]
          (let [change-queue (om/get-state owner :change-queue)
                delete-queue (om/get-state owner :delete-queue)]
            (go (loop []
                  
                  (let [change-cursor (<! change-queue)
                        to-change  @change-cursor
                        new-state (not (:done  to-change))]
                    (om/transact! app :todos
                                  (update-item-state to-change new-state ))
                    ;;deref again to get the new state
                    (>! delete-queue @change-cursor)
                    (recur))))
            (go (loop []
                  (let [to-delete (<! delete-queue)
                        now (<! (timeout 2000))]
                    (if to-delete
                      (delete-item app  to-delete))
                    (recur))))))
        om/IRenderState
        (render-state [_ state]
          (dom/div nil
                   (dom/h1 nil (:text app))
                   (dom/input #js {:type "text" :ref "new-item"})
                   (dom/button #js {:onClick #(add-item app owner) } "Add")
                   (apply dom/ol nil
                          (om/build-all todo-item-view (:todos app)
                                        {:init-state state}))))))
    app-state
    {:target (. js/document (getElementById "app"))}))
