(ns clojure-dojo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! timeout]]
            [clojure.string :as string]))

(defonce app-state (atom {:text "Hello Chestnut!"
                          :todos ["Milch kaufen" "Auto waschen"]}))

(defn dom-todo-item [todo]
  (dom/li nil todo)
)

(defn main []
  (om/root
    (fn [app owner]
      (reify
       om/IRenderState
        (render-state [_ state]
          (dom/div nil
                   (dom/h1 nil (:text app))
                   (apply dom/ul nil 
                            (map dom-todo-item (:todos app)))))
      )
    )
    app-state
    {:target (. js/document (getElementById "app"))}
  )
)
