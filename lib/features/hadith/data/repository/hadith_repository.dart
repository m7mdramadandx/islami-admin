import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:islami_admin/features/hadith/domain/entities/hadith.dart';

class HadithRepository {
  final CollectionReference _hadithCollection = FirebaseFirestore.instance.collection('hadiths');

  Future<void> addHadith(Hadith hadith) async {
    await _hadithCollection.doc(hadith.id).set(hadith.toJson());
  }

  Future<void> updateHadith(Hadith hadith) async {
    await _hadithCollection.doc(hadith.id).update(hadith.toJson());
  }

  Future<void> deleteHadith(String id) async {
    await _hadithCollection.doc(id).delete();
  }

  Stream<List<Hadith>> getHadiths() {
    return _hadithCollection.snapshots().map((snapshot) {
      return snapshot.docs.map((doc) {
        return Hadith.fromJson(doc.data() as Map<String, dynamic>);
      }).toList();
    });
  }
}
