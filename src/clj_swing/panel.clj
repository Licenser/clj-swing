(ns clj-swing.panel
  (:use [clj-swing.core :only [group-container-args auto-setters icon-setters]])
  (:use [clojure.contrib.swing-utils :only [do-swing]])
  (:import (javax.swing Box BoxLayout JPanel JScrollPane JSplitPane)
	   (java.awt Dimension Component GridLayout FlowLayout))
  (:require [clojure.contrib.java-utils :as java]))


(def *panel-known-keys*
     [:name :icon :title :layout :constrains :size :bounds :location :pack :preferred-size :paint])


(defmacro general-panel [cl args]
  "options are:
:name - internal name of the frame.
:layout - layout manager.
:constrains - constrains object for the layout manager.
:on-close - one of :do-nothing, :exit, :hide, :dispose, 
  sets the default on close action for the frame.
:preferred-size - [w, h]
:bounds - [x, y, w, h]
:location - [x y]
:paint - paint function
:pack - shall the frame autopack at the end?
:show - shall the frame autoshow at the end?
"
    (let [default-opts {}
	  {forms :forms {[[paint-obj] & paint-code] :paint :as opts} :options bindings :bindings} (group-container-args args)
	  opts (merge default-opts opts)
	  panel (or (:name opts) (gensym "panel"))
	  constrains (gensym "constrains")
	  manager (gensym "manager")]
      `(let [~panel  ~(if (:paint opts)
			`(proxy [~cl] []
			  (paintComponent [~paint-obj] 
					  ~@paint-code))
			`(new ~cl))
	     ~@(if (:layout opts)
		 ['_ `(.setLayout ~panel ~(:layout opts))])
	     ~@(if (:constrains opts)
		 `(~constrains ~(:constrains opts)))
	     ~@(if (:constrains opts)
		 (reverse 
		  (reduce
		   (fn [l [f s]]
		     (if (keyword? f)
		       (conj (conj l '_) `(set-constraint! ~constrains ~f ~s))
		       (conj (conj (conj (conj l f) s) '_) `(.add ~panel ~f ~constrains))))
		   '() (partition 2 bindings)))
		 (reverse 
		  (reduce
		   (fn [l [f s]]
		     (conj (conj (conj (conj l f) s) '_) `(.add ~panel ~f)))
		   '() (partition 2 bindings))))]
	 (doto ~panel
	   ~@(icon-setters [:icon]  opts)
	   ~@(auto-setters cl *panel-known-keys* opts)
	   ~@(when-let [[w h] (:preferred-size opts)]
	      [`(.setPreferredSize (Dimension. ~w ~h))])
	   ~@(when-let [[x y w h] (:bounds opts)]
	      [`(.setBounds ~x ~y ~w ~h)])
	   ~@(when-let [[x y] (:location opts)]
	      [`(.setLocation ~x ~y)])
	   ~@forms))))

(defmacro panel [& args]
  `(general-panel JPanel ~args))

(defmacro split-horizontal [left right]
  `(JSplitPane. JSplitPane/HORIZONTAL_SPLIT
	       ~left ~right))

(defmacro split-vertical [top bottom]
  `(JSplitPane. JSplitPane/VERTICAL_SPLIT
	       ~top ~bottom))


; Idea from joy of clojure

(defmacro shelf [[& bindings] & body]
  (let [pan (gensym "stack")]
    `(let [~pan (doto (JPanel.) (.setLayout (FlowLayout.))) 
	   ~@(reduce
	     (fn [l [f s]]
	       (concat l 
		       (list f s)
		       (list '_ `(.add ~pan ~f))))
	     '() (partition 2 bindings))]
       ~@body
       ~pan)))

(defmacro stack [[& bindings] & body]
  (let [st (gensym "stack")]
    `(let [~st (Box. BoxLayout/PAGE_AXIS)
	   ~@(reduce
	     (fn [l [f s]]
	       (concat l 
		       (list f s)
		       (list '_ `(.add ~st (doto ~f (.setAlignmentX Component/CENTER_ALIGNMENT))))))
	     '() (partition 2 bindings))]
       ~@body
       ~st)))

(defmacro scroll-panel [obj & { :as opts}]
  `(doto (new JScrollPane ~obj)
     ~@(auto-setters JScrollPane [:preferred-size] opts)
     ~@(when-let [[w h] (:preferred-size opts)]
	 [`(.setPreferredSize (Dimension. ~w ~h))])))


