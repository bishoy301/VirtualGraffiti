#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use] extern crate rocket;

use rocket::http::RawStr;

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
