#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use] extern crate rocket;
#[macro_use] extern crate rocket_contrib;
#[macro_use] extern crate serde_derive;

use rocket::http::RawStr;
use rocket_contrib::{Json, value};

#[get("/")]
fn index() -> &'static str {
    "Hello, world!"
}

#[get("/images/<gps>")]
fn set_gps(gps: &RawStr) -> String {
    format!("The gps location is {}", gps.as_str())
}

fn main() {
    rocket::ignite()
        .mount("/", routes![index, set_gps])
        .launch();
}
