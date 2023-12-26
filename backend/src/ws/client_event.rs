use serde::Deserialize;

use super::call_state::{IncomingCallOffer, CallResponse, WebRTCPayload};

#[derive(Deserialize, Debug)]
#[serde(tag = "event", content = "data")]
#[typeshare::typeshare]
pub enum ClientEvent {
  CallOffer(IncomingCallOffer),
  CallResponse(CallResponse),
  WebRTCPayload(WebRTCPayload),
}
