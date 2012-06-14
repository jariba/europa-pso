(define (problem logistics-4-0)

(:domain logistics-object-fluents)

(:objects  apn1 - airplane
           tru1 tru2 - truck
           obj11 obj12 obj13 obj21 obj22 obj23 - package
           apt1 apt2 - airport
           pos1 pos2 - location
           cit1 cit2 - city)

(:init  (= (vehicle-location apn1) apt2)
        (= (vehicle-location tru1) pos1)
        (= (vehicle-location tru2) pos2)
        (= (package-location obj11) pos1)
        (= (package-location obj12) pos1)
        (= (package-location obj13) pos1)
        (= (package-location obj21) pos2)
        (= (package-location obj22) pos2)
        (= (package-location obj23) pos2)
        (= (city-of apt1) cit1)
        (= (city-of apt2) cit2)
        (= (city-of pos1) cit1)
        (= (city-of pos2) cit2))

(:goal  (and (= (package-location obj11) apt1)
             (= (package-location obj13) apt1)
             (= (package-location obj21) pos1)
             (= (package-location obj23) pos1)))

)
