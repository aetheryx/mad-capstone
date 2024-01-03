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
  user_id: i32,
  call_offer: IncomingCallOffer,
  state: SharedState
) -> Result<()> {
  let outgoing_call_offer = OutgoingCallOffer {
    conversation_id: call_offer.conversation_id,
    call_id: call_offer.call_id,
  };

  let event = ServerEvent::CallOffer(outgoing_call_offer);
  event.send_to(&state, call_offer.callee_id).await?;

  let mut call = CALL.lock().await;
  *call = Some(Call {
    caller: user_id,
    callee: call_offer.callee_id,
  });

  Ok(())
}

pub async fn handle_call_response(
  call_response: CallResponse,
  state: SharedState,
) -> Result<()> {
  let event = ServerEvent::CallResponse(call_response);
  event.send_to(&state, call_response.caller_id).await
}

pub async fn handle_call_hangup(
  user_id: i32,
  call_id: u32,
  state: SharedState,
) -> Result<()> {
  let Some(target_id) = get_target_id(user_id).await else {
    return Ok(());
  };

  let event = ServerEvent::CallHangUp(call_id);
  event.send_to(&state, target_id).await?;

  Ok(())
}

pub async fn handle_webrtc_payload(
  user_id: i32,
  payload: WebRTCPayload,
  state: SharedState,
) -> Result<()> {
  let Some(target_id) = get_target_id(user_id).await else {
    return Ok(());
  };

  let event = ServerEvent::WebRTCPayload(payload);
  event.send_to(&state, target_id).await?;

  println!("sent {event:?} to {target_id}");

  Ok(())
}

async fn get_target_id(user_id: i32) -> Option<i32> {
  let Some(call) = CALL.lock().await.clone() else {
    return None;
  };

  if user_id == call.callee {
    Some(call.caller)
  } else {
    Some(call.callee)
  }
}

#[derive(Clone, Copy, Debug)]
struct Call {
  caller: i32,
  callee: i32,
}
