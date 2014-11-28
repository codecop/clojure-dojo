(ns clojure-dojo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! timeout]]
            [clojure.string :as string]))

(defonce app-state
  (atom {:text "All the things I have to do:"
         :todos ["buy milk" "wash car"]}
  )
)

(defn dom-todo-item [todo]
  (dom/li nil todo)
)

(defn om-todo-item [todo owner]
  (om/component
    (dom-todo-item todo)
  )
)

(defn clear-all [app]
  (om/update! app :todos [] )
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
              (om/build-all om-todo-item (:todos app)))
            (dom/button #js {:onClick (fn [e] (clear-all app)) }
                        "I have done everything! (Clear)")
          )
        )
      )
    )
    app-state
    {:target (. js/document (getElementById "app"))}
  )
)
