(ns plexus-demo.repl
  (:require 
   [plexus.core :as p]
   [clj-manifold3d.core :as m]))

(defn export-model [model]
  (p/export
   (if (m/cross-section? model)
     (m/extrude model 1/2)
     model)
   "model.glb"
   (m/material :color [2.0 61 59 1.0] :metalness 0.2)))