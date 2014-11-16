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
    (render-state [_ {:keys [comm]}]
      (dom/li nil
              (dom/div nil
               (dom/input #js{:type "checkbox"
                              :checked (:done item)
                              :onClick (fn [e] (om/transact! item :done #(not %) ))})
               (dom/label nil (:text item))
               (dom/button #js {:onClick (fn [_] (put! comm [:delete @item]))} "x"))))))


(defn add-item [app owner]
  (let [input (om/get-node owner "new-item")
        new-item (.-value input)]
    (when (and  new-item (not (string/blank? new-item)))
      (om/transact! app :todos #(conj % {:text new-item :done false}))
      (set! (.-value input) "")
      (.focus input))))

(defn delete-item [item app]
  (om/transact! app :todos
              (fn [todos] (vec (remove #(= item %) todos)))))


(defn handle-event [event value app]
  (case event
    :delete (delete-item value app)))



(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IInitState
        (init-state [_]
          {:comm (chan)})
        om/IWillMount
        (will-mount [_]
          (let [comm (om/get-state owner :comm)]
            (go (while true
                  (let [[event value] (<! comm)]
                    (handle-event event value app))))))
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
