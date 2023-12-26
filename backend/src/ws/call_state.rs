use serde::{Deserialize, Serialize};
use typeshare::typeshare;

use crate::db::entities::user;

#[derive(Deserialize, Serialize, Debug, Clone, Copy)]
#[typeshare]
pub struct IncomingCallOffer {
  pub callee_id: i32,
}

#[derive(Serialize, Debug)]
#[typeshare]
pub struct OutgoingCallOffer {
  pub callee: user::Model,
}

#[derive(Deserialize, Serialize, Debug, Clone, Copy)]
#[typeshare]
pub struct CallResponse {
  pub caller_id: i32,
  pub accepted: bool,
}

#[derive(Deserialize, Serialize, Debug)]
#[typeshare]
pub struct WebRTCPayload {
  pub payload: String,
}
