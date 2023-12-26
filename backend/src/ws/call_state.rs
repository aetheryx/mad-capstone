use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize, Debug, Clone, Copy)]
pub struct CallOffer {
  pub callee: i32,
}

#[derive(Deserialize, Serialize, Debug, Clone, Copy)]
pub struct CallResponse {
  pub caller: i32,
  pub accepted: bool,
}

#[derive(Deserialize, Serialize, Debug)]
pub struct WebRTCPayload {
  pub payload: String,
}
