CREATE TABLE "categorys" (
                             "id" integer PRIMARY KEY,
                             "name" varchar
);

CREATE TABLE "users" (
                         "id" integer PRIMARY KEY,
                         "username" varchar,
                         "email" varchar
);

CREATE TABLE "events" (
                          "id" integer PRIMARY KEY,
                          "category_id" integer,
                          "user_id" integer NOT NULL,
                          "title" varchar,
                          "annotation" varchar,
                          "description" varchar,
                          "eventDate" timestamp,
                          "location_lat" float,
                          "location_lon" float,
                          "paid" bool,
                          "participantLimit" integer,
                          "requestModeration" bool
);

CREATE TABLE "requests" (
                            "id" integer PRIMARY KEY,
                            "event" integer,
                            "requester" integer,
                            "status" VARCHAR,
                            "created" timestamp
);

CREATE TABLE "compilations" (
                                "id" integer PRIMARY KEY,
                                "title" varchar,
                                "pinned" bool
);

CREATE TABLE "eventlinks" (
                              "event_id" integer,
                              "compiation_id" integer
);

ALTER TABLE "events" ADD CONSTRAINT "user_events" FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "events" ADD CONSTRAINT "category_events" FOREIGN KEY ("category_id") REFERENCES "categorys" ("id");

ALTER TABLE "requests" ADD CONSTRAINT "request_events" FOREIGN KEY ("id") REFERENCES "events" ("id");

ALTER TABLE "requests" ADD CONSTRAINT "user_requests" FOREIGN KEY ("id") REFERENCES "users" ("id");

ALTER TABLE "eventlinks" ADD CONSTRAINT "events_links" FOREIGN KEY ("event_id") REFERENCES "events" ("id");

ALTER TABLE "eventlinks" ADD CONSTRAINT "compilation_links" FOREIGN KEY ("compiation_id") REFERENCES "compilations" ("id");
