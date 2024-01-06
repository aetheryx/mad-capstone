//! `SeaORM` Entity. Generated by sea-orm-codegen 0.12.6

use sea_orm::entity::prelude::*;
use serde::Serialize;

#[derive(Clone, Debug, PartialEq, DeriveEntityModel, Eq, Serialize)]
#[sea_orm(table_name = "conversation_message")]
#[typeshare::typeshare]
pub struct Model {
  #[sea_orm(primary_key)]
  pub id: i32,
  pub author_id: i32,
  pub conversation_id: i32,
  pub created_at: DateTime,
  #[sea_orm(column_type = "Text")]
  pub content: String,
  pub reply_to_id: Option<i32>,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveRelation)]
pub enum Relation {
  #[sea_orm(
    belongs_to = "super::conversation::Entity",
    from = "Column::ConversationId",
    to = "super::conversation::Column::Id",
    on_update = "Cascade",
    on_delete = "Cascade"
  )]
  Conversation,
  #[sea_orm(
    belongs_to = "Entity",
    from = "Column::ReplyToId",
    to = "Column::Id",
    on_update = "Cascade",
    on_delete = "SetNull"
  )]
  SelfRef,
  #[sea_orm(
    belongs_to = "super::user::Entity",
    from = "Column::AuthorId",
    to = "super::user::Column::Id",
    on_update = "Cascade",
    on_delete = "Restrict"
  )]
  User,
}

impl Related<super::conversation::Entity> for Entity {
  fn to() -> RelationDef {
    Relation::Conversation.def()
  }
}

impl Related<super::user::Entity> for Entity {
  fn to() -> RelationDef {
    Relation::User.def()
  }
}

impl ActiveModelBehavior for ActiveModel {}
