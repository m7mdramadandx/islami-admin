import 'package:firebase_auth/firebase_auth.dart' as firebase;
import 'package:islami_admin/features/auth/domain/entities/user_entity.dart';

class UserModel extends UserEntity {
  const UserModel({required super.uid, super.email});

  factory UserModel.fromFirebaseUser(firebase.User user) {
    return UserModel(uid: user.uid, email: user.email);
  }
}
