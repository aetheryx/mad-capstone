use serde::Deserialize;

use super::call_state::{CallOffer, CallResponse, WebRTCPayload};

#[derive(Deserialize, Debug)]
#[serde(tag = "event", content = "data")]
pub enum IncomingWebsocketEvent {
  CallOffer(CallOffer),
  CallResponse(CallResponse),
  WebRTCPayload(WebRTCPayload),
}
