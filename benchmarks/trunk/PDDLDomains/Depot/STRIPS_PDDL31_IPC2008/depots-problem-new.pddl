(define (problem depotprob1818)
  (:domain Depot-object-fluents)
  (:objects depot0 - Depot
	    distributor0 distributor1 - Distributor
	    truck0 truck1 - Truck
	    pallet0 pallet1 pallet2 - Pallet
	    crate0 crate1 - Crate
	    hoist0 hoist1 hoist2 - Hoist)
  (:init
   (= (position-of pallet0) depot0)
   (clear crate1)
   (= (position-of pallet1) distributor0)
   (clear crate0)
   (= (position-of pallet2) distributor1)
   (clear pallet2)
   (= (position-of truck0) distributor1)
   (= (current-load truck0) 0)
   (= (load-limit truck0) 323)
   (= (position-of truck1) depot0)
   (= (current-load truck1) 0)
   (= (load-limit truck1) 220)
   (= (position-of hoist0) depot0)
   (= (crate-held hoist0) no-crate)
   (= (position-of hoist1) distributor0)
   (= (crate-held hoist1) no-crate)
   (= (position-of hoist2) distributor1)
   (= (crate-held hoist2) no-crate)
   (= (position-of crate0) distributor0)
   (= (thing-below crate0) pallet1)
   (= (weight crate0) 11)
   (= (position-of crate1) depot0)
   (= (thing-below crate1) pallet0)
   (= (weight crate1) 86)
   (= (fuel-cost) 0))

  (:goal (and (= (thing-below crate0) pallet2)
	      (= (thing-below crate1) pallet1)))

  (:metric minimize (fuel-cost))
  )
