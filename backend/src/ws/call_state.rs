use serde::{Deserialize, Serialize};
use typeshare::typeshare;

#[derive(Deserialize, Serialize, Debug, Clone, Copy)]
#[typeshare]
pub struct IncomingCallOffer {
  pub callee_id: i32,
  pub conversation_id: i32,
  pub call_id: u32,
}

#[derive(Serialize, Debug)]
#[typeshare]
pub struct OutgoingCallOffer {
  pub conversation_id: i32,
  pub call_id: u32,
}

#[derive(Deserialize, Serialize, Debug, Clone, Copy)]
#[typeshare]
pub struct CallResponse {
  pub caller_id: i32,
  pub accepted: bool,
  pub call_id: u32,
}

#[derive(Deserialize, Serialize, Debug)]
#[typeshare]
pub struct WebRTCPayload {
  pub payload: String,
}
