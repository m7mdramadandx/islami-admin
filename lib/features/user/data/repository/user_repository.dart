import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:islami_admin/features/user/domain/entities/user.dart';

class UserRepository {
  final CollectionReference _userCollection = FirebaseFirestore.instance
      .collection('users');

  Future<void> addUser(User user) async {
    await _userCollection.doc(user.id).set(user.toJson());
  }

  Future<void> updateUser(User user) async {
    await _userCollection.doc(user.id).update(user.toJson());
  }

  Future<void> deleteUser(String id) async {
    await _userCollection.doc(id).delete();
  }

  Stream<List<User>> getUsers() {
    return _userCollection.snapshots().map((snapshot) {
      return snapshot.docs.map((doc) {
        return User.fromJson(doc.data() as Map<String, dynamic>);
      }).toList();
    });
  }
}
