import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:islami_admin/features/quran/domain/entities/surah.dart';

class QuranRepository {
  final String _baseUrl = 'https://api.alquran.cloud/v1';

  Future<Surah> getSurah(int surahNumber) async {
    final response = await http.get(Uri.parse('$_baseUrl/surah/$surahNumber'));

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      return Surah.fromJson(data['data']);
    } else {
      throw Exception('Failed to load surah');
    }
  }
}
