use anyhow::{Ok, Result};
use futures::lock::Mutex;

use crate::{
  ws::{call_state::*, ServerEvent},
  SharedState,
};

lazy_static! {
  static ref CALL: Mutex<Option<Call>> = Mutex::from(None);
}

pub async fn handle_call_offer(
  call_offer: IncomingCallOffer,
  state: SharedState,
) -> Result<()> {
  let outgoing_call_offer = OutgoingCallOffer {
    conversation_id: call_offer.conversation_id
  };

  let event = ServerEvent::CallOffer(outgoing_call_offer);
  event.send_to(&state, call_offer.callee_id).await
}

pub async fn handle_call_response(
  user_id: i32,
  call_response: CallResponse,
  state: SharedState,
) -> Result<()> {
  if call_response.accepted {
    let mut call = CALL.lock().await;
    *call = Some(Call {
      caller: call_response.caller_id,
      callee: user_id,
    });
  }

  let event = ServerEvent::CallResponse(call_response);
  event.send_to(&state, call_response.caller_id).await
}

pub async fn handle_webrtc_payload(
  user_id: i32,
  payload: WebRTCPayload,
  state: SharedState,
) -> Result<()> {
  let Some(call) = CALL.lock().await.clone() else {
    return Ok(());
  };

  let target_id = if user_id == call.callee { call.caller } else { call.callee };

  let event = ServerEvent::WebRTCPayload(payload);
  event.send_to(&state, target_id).await?;

  println!("sent {event:?} to {target_id}");

  Ok(())
}

#[derive(Clone, Copy, Debug)]
struct Call {
  caller: i32,
  callee: i32,
}
