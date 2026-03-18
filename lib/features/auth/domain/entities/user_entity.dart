import 'package:equatable/equatable.dart';
import 'package:firebase_auth/firebase_auth.dart' as firebase;

class UserEntity extends Equatable {
  final String uid;
  final String? email;

  const UserEntity({required this.uid, this.email});

  factory UserEntity.fromFirebaseUser(firebase.User user) {
    return UserEntity(uid: user.uid, email: user.email);
  }

  @override
  List<Object?> get props => [uid, email];
}
