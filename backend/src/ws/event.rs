use crate::{db::entities::*, SharedState};
use axum::extract::ws::Message;
use futures::SinkExt;
use serde::{ser::SerializeStruct, Serialize, Serializer};

pub enum WebsocketEvent<'a> {
  MessageCreate(&'a conversation_message::Model),
}

impl<'a> Serialize for WebsocketEvent<'a> {
  fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
  where
    S: Serializer,
  {
    let (event, data) = match *self {
      Self::MessageCreate(i) => ("MESSAGE_CREATE", i),
    };

    let mut state = serializer.serialize_struct("WebsocketEvent", 2)?;
    state.serialize_field("event", event)?;
    state.serialize_field("data", data)?;
    state.end()
  }
}

impl<'a> WebsocketEvent<'a> {
  pub async fn send_to(&self, state: &SharedState, id: i32) -> anyhow::Result<()> {
    let msg = Message::Binary(serde_json::to_vec(&self)?);

    let mut clients = state.clients.lock().await;
    let client = clients.get_mut(&id).unwrap();
    client.send(msg).await?;

    Ok(())
  }
}
