import 'package:flutter/material.dart';
import 'package:islami_admin/features/quran/data/repositories/quran_repository.dart';
import 'package:islami_admin/features/quran/domain/entities/surah.dart';

class QuranManagementPage extends StatefulWidget {
  const QuranManagementPage({super.key});

  @override
  State<QuranManagementPage> createState() => _QuranManagementPageState();
}

class _QuranManagementPageState extends State<QuranManagementPage> {
  final QuranRepository _quranRepository = QuranRepository();
  late Future<List<Surah>> _surahs;

  @override
  void initState() {
    super.initState();
    _surahs = _fetchAllSurahs();
  }

  Future<List<Surah>> _fetchAllSurahs() async {
    final List<Future<Surah>> surahFutures = [];
    for (int i = 1; i <= 114; i++) {
      surahFutures.add(_quranRepository.getSurah(i));
    }
    return Future.wait(surahFutures);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Quran Management'),
      ),
      body: FutureBuilder<List<Surah>>(
        future: _surahs,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            final surahs = snapshot.data!;
            return ListView.builder(
              itemCount: surahs.length,
              itemBuilder: (context, index) {
                final surah = surahs[index];
                return ListTile(
                  title: Text(surah.name),
                  subtitle: Text(surah.englishName),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => SurahDetailPage(surah: surah),
                      ),
                    );
                  },
                );
              },
            );
          } else if (snapshot.hasError) {
            return Center(
              child: Text('Error: ${snapshot.error}'),
            );
          } else {
            return const Center(
              child: CircularProgressIndicator(),
            );
          }
        },
      ),
    );
  }
}

class SurahDetailPage extends StatelessWidget {
  final Surah surah;

  const SurahDetailPage({super.key, required this.surah});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(surah.name),
      ),
      body: ListView.builder(
        itemCount: surah.ayahs.length,
        itemBuilder: (context, index) {
          final ayah = surah.ayahs[index];
          return ListTile(
            title: Text(ayah.text),
            subtitle: Text('Ayah ${ayah.numberInSurah}'),
          );
        },
      ),
    );
  }
}
