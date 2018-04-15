--For Function(1), Function(2) ::
CREATE INDEX passNum_indx ON Passenger USING hash(passNum);

CREATE INDEX FflightNum_indx ON Flight USING hash (flightNum);

CREATE INDEX BflightNum_indx ON Booking USING hash (flighNum);

CREATE INDEX BbookRef_indx ON Booking USING hash (bookRef);

CREATE INDEX BpID_indx ON Booking USING hash (pID);

CREATE INDEX Bdeparture_indx ON Booking USING hash (departure);

CREATE INDEX Bdeparture2_indx ON Booking USING hash (departure,flightNum);

CREATE INDEX RpID_indx ON Ratings USING hash(pID);

CREATE INDEX RflightNum_indx ON Ratings USING hash (flightNum);

CREATE INDEX Flightdest_indx ON Flight USING hash(origin,destination);

CREATE INDEX AirID_indx ON Airline USING hash (airId);

