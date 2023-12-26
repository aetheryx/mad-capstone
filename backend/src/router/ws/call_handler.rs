use anyhow::{Context, Ok, Result};
use futures::lock::Mutex;
use sea_orm::EntityTrait;

use crate::{
  db::entities::user,
  ws::{call_state::*, ServerEvent},
  SharedState,
};

lazy_static! {
  static ref CALL: Mutex<Option<Call>> = Mutex::from(None);
}

pub async fn handle_call_offer(
  id: i32,
  call_offer: IncomingCallOffer,
  state: SharedState,
) -> Result<()> {
  let outgoing_call_offer = OutgoingCallOffer {
    callee: user::Entity::find_by_id(id)
      .one(&state.db)
      .await?
      .context("callee not found")?,
  };

  let event = ServerEvent::CallOffer(outgoing_call_offer);
  event.send_to(&state, call_offer.callee_id).await
}

pub async fn handle_call_response(
  id: i32,
  call_response: CallResponse,
  state: SharedState,
) -> Result<()> {
  if call_response.accepted {
    let mut call = CALL.lock().await;
    *call = Some(Call {
      caller: call_response.caller_id,
      callee: id,
    });
  }

  let event = ServerEvent::CallResponse(call_response);
  event.send_to(&state, call_response.caller_id).await
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
