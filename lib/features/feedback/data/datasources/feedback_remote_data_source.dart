import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:islami_admin/features/feedback/domain/entities/feedback_message.dart';

abstract class FeedbackRemoteDataSource {
  Future<List<FeedbackMessage>> getFeedbackMessages();
}

class FeedbackRemoteDataSourceImpl implements FeedbackRemoteDataSource {
  final FirebaseFirestore firestore;

  FeedbackRemoteDataSourceImpl({required this.firestore});

  @override
  Future<List<FeedbackMessage>> getFeedbackMessages() async {
    try {
      final snapshot = await firestore
          .collection('root')
          .doc('feedback')
          .collection('messages')
          .orderBy('date', descending: true)
          .get();

      return snapshot.docs
          .map((doc) => FeedbackMessage.fromFirestore(doc.data(), doc.id))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch feedback messages: $e');
    }
  }
}
