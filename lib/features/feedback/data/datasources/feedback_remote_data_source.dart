import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:islami_admin/features/feedback/data/models/feedback_model.dart';

abstract class FeedbackRemoteDataSource {
  Future<List<FeedbackModel>> getFeedbackMessages();
}

class FeedbackRemoteDataSourceImpl implements FeedbackRemoteDataSource {
  final FirebaseFirestore firestore;

  FeedbackRemoteDataSourceImpl({required this.firestore});

  @override
  Future<List<FeedbackModel>> getFeedbackMessages() async {
    try {
      final snapshot = await firestore
          .collection('root')
          .doc('feedback')
          .collection('messages')
          .orderBy('date', descending: true)
          .get();

      return snapshot.docs
          .map((doc) => FeedbackModel.fromFirestore(doc.data(), doc.id))
          .toList();
    } catch (e) {
      throw Exception('Failed to fetch feedback messages: $e');
    }
  }
}
