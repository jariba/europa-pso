(define (domain Depot-object-fluents)
  (:requirements :typing :equality :fluents)
  (:types place locatable - object
	  depot distributor - place
	  truck hoist surface - locatable
	  pallet crate - surface)

  (:constants no-crate - crate)

  (:predicates
   (clear ?s - surface))

  (:functions
   (load-limit ?t - truck)
   (current-load ?t - truck)
   (weight ?c - crate)
   (fuel-cost) - number
   (position-of ?l - locatable) - place
   (crate-held ?h - hoist) - crate
   (thing-below ?c - crate) - (either surface truck))


  (:action drive
   :parameters (?t - truck ?p - place)
   :effect (and (assign (position-of ?t) ?p)
		(increase (fuel-cost) 10)))

  (:action lift
   :parameters (?h - hoist ?c - crate)
   :precondition (and (= (position-of ?h) (position-of ?c))
		      (= (crate-held ?h) no-crate)
		      (clear ?c))
   :effect (and (assign (crate-held ?h) ?c)
                (assign (position-of ?c) undefined)
		(assign (thing-below ?c) undefined)
		(clear (thing-below ?c))
		(increase (fuel-cost) 1)))

  (:action drop
   :parameters (?h - hoist ?c - crate ?s - surface)
   :precondition (and (= (position-of ?h) (position-of ?s))
                      (= (crate-held ?h) ?c)
		      (clear ?s))
   :effect (and (assign (crate-held ?h) no-crate)
                (assign (position-of ?c) (position-of ?h))
		(assign (thing-below ?c) ?s)
		(not (clear ?s))))

  (:action load
   :parameters (?h - hoist ?c - crate ?t - truck)
   :precondition (and (= (position-of ?h) (position-of ?t))
		      (= (crate-held ?h) ?c)
		      (<= (+ (current-load ?t) (weight ?c))
			  (load-limit ?t)))
   :effect (and (assign (crate-held ?h) no-crate)
		(assign (thing-below ?c) ?t)
		(increase (current-load ?t) (weight ?c))))

  (:action unload
   :parameters (?h - hoist ?c - crate ?t - truck)
   :precondition (and (= (position-of ?h) (position-of ?t))
		      (= (crate-held ?h) no-crate)
		      (= (thing-below ?c) ?t))
   :effect (and (assign (thing-below ?c) undefined)
		(assign (crate-held ?h) ?c)
		(decrease (current-load ?t) (weight ?c))))
  )
