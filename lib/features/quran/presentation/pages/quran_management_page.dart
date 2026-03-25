import 'package:flutter/material.dart';
import 'package:islami_admin/core/utils/colors.dart';
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
        centerTitle: true,
      ),
      body: FutureBuilder<List<Surah>>(
        future: _surahs,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            final surahs = snapshot.data!;
            return ListView.builder(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
              itemCount: surahs.length,
              itemBuilder: (context, index) {
                final surah = surahs[index];
                return Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Card(
                    clipBehavior: Clip.antiAlias,
                    child: ListTile(
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 20,
                        vertical: 12,
                      ),
                      leading: CircleAvatar(
                        backgroundColor:
                            AppColors.colorPrimary.withValues(alpha: 0.12),
                        child: Text(
                          '${surah.number}',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: AppColors.colorPrimary,
                            fontSize: 13,
                          ),
                        ),
                      ),
                      title: Text(
                        surah.name,
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: AppColors.text,
                        ),
                      ),
                      subtitle: Text(
                        surah.englishName,
                        style: TextStyle(
                          fontSize: 13,
                          color: AppColors.subText,
                        ),
                      ),
                      trailing: Icon(
                        Icons.chevron_right_rounded,
                        color: AppColors.grey,
                      ),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => SurahDetailPage(surah: surah),
                          ),
                        );
                      },
                    ),
                  ),
                );
              },
            );
          } else if (snapshot.hasError) {
            return Center(
              child: Card(
                margin: const EdgeInsets.all(24),
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Text(
                    'Error: ${snapshot.error}',
                    style: TextStyle(color: AppColors.failureRed),
                  ),
                ),
              ),
            );
          } else {
            return const Center(child: CircularProgressIndicator());
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
        centerTitle: true,
      ),
      body: ListView.builder(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
        itemCount: surah.ayahs.length,
        itemBuilder: (context, index) {
          final ayah = surah.ayahs[index];
          return Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: Card(
              clipBehavior: Clip.antiAlias,
              child: ListTile(
                contentPadding: const EdgeInsets.all(16),
                leading: CircleAvatar(
                  backgroundColor:
                      AppColors.colorAccent.withValues(alpha: 0.2),
                  child: Text(
                    '${ayah.numberInSurah}',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: AppColors.titleColor,
                    ),
                  ),
                ),
                title: Text(
                  ayah.text,
                  textDirection: TextDirection.rtl,
                  style: TextStyle(
                    fontSize: 18,
                    height: 1.8,
                    color: AppColors.text,
                  ),
                ),
                subtitle: Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Text(
                    'Ayah ${ayah.numberInSurah}',
                    style: TextStyle(
                      fontSize: 12,
                      color: AppColors.subText,
                    ),
                  ),
                ),
                isThreeLine: true,
              ),
            ),
          );
        },
      ),
    );
  }
}
