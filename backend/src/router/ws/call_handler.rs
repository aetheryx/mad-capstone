use anyhow::{Context, Ok, Result};
use futures::lock::Mutex;

use crate::{
  ws::{call_state::*, OutgoingWebsocketEvent},
  SharedState,
};

lazy_static! {
  static ref CALL: Mutex<Option<Call>> = Mutex::from(None);
}

pub async fn handle_call_offer(call_offer: CallOffer, state: SharedState) -> Result<()> {
  let event = OutgoingWebsocketEvent::CallOffer(call_offer);
  event.send_to(&state, call_offer.callee).await?;

  Ok(())
}

pub async fn handle_call_response(
  id: i32,
  call_response: CallResponse,
  state: SharedState,
) -> Result<()> {
  let event = OutgoingWebsocketEvent::CallResponse(call_response);
  event.send_to(&state, call_response.caller).await?;

  if call_response.accepted {
    let mut call = CALL.lock().await;
    *call = Some(Call {
      caller: call_response.caller,
      callee: id,
    });
  }

  Ok(())
}

pub async fn handle_webrtc_payload(
  id: i32,
  payload: WebRTCPayload,
  state: SharedState,
) -> Result<()> {
  let Some(call) = CALL.lock().await.clone() else {
    return Ok(());
  };

  let target_id = if id == call.callee { call.caller } else { call.callee };

  let event = OutgoingWebsocketEvent::WebRTCPayload(payload);
  event.send_to(&state, target_id).await?;

  println!("sent {event:?} to {target_id}");

  Ok(())
}

#[derive(Clone, Copy, Debug)]
struct Call {
  caller: i32,
  callee: i32,
}
