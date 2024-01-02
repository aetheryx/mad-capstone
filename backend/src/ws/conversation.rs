use serde::Serialize;
use typeshare::typeshare;

use crate::db::entities::*;

#[derive(Serialize, Debug)]
#[typeshare]
pub struct FullConversation {
  pub conversation: conversation::Model,
  pub other_participant: user::Model,
  pub last_message: Option<conversation_message::Model>,
}
