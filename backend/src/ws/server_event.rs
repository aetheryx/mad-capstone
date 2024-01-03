use crate::{db::entities::*, SharedState};
use axum::extract::ws::Message;
use futures::SinkExt;
use serde::Serialize;

use super::call_state::{CallResponse, WebRTCPayload, OutgoingCallOffer};
use super::conversation::FullConversation;

#[derive(Serialize, Debug)]
#[serde(tag = "event", content = "data")]
#[typeshare::typeshare]
pub enum ServerEvent<'a> {
  MessageCreate(&'a conversation_message::Model),
  ConversationCreate(&'a FullConversation),
  ConversationDelete(i32),
  CallOffer(OutgoingCallOffer),
  CallResponse(CallResponse),
  WebRTCPayload(WebRTCPayload),
  CallHangUp(u32),
}

impl<'a> ServerEvent<'a> {
  pub async fn send_to(&self, state: &SharedState, id: i32) -> anyhow::Result<()> {
    let msg = Message::Binary(serde_json::to_vec(&self)?);

    let mut users = state.users.lock().await;
    let Some(connections) = users.get_mut(&id) else {
      return Ok(());
    };

    println!("sending event {self:?} to {id:?}");

    for client in connections.values_mut() {
      let _ = client.send(msg.clone()).await;
    }

    Ok(())
  }
}
