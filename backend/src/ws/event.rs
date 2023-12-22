use crate::{db::entities::*, SharedState};
use axum::extract::ws::Message;
use futures::SinkExt;
use serde::Serialize;

#[derive(Serialize)]
#[serde(tag = "event", content = "data")]
#[typeshare::typeshare]
pub enum WebsocketEvent<'a> {
  MessageCreate(&'a conversation_message::Model),
}

impl<'a> WebsocketEvent<'a> {
  pub async fn send_to(&self, state: &SharedState, id: i32) -> anyhow::Result<()> {
    let msg = Message::Binary(serde_json::to_vec(&self)?);

    let mut clients = state.clients.lock().await;
    if let Some(client) = clients.get_mut(&id) {
      client.send(msg).await?;
    }

    Ok(())
  }
}
