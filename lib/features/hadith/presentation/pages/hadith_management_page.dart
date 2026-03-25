import 'package:flutter/material.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/hadith/data/repository/hadith_repository.dart';
import 'package:islami_admin/features/hadith/domain/entities/hadith.dart';

class HadithManagementPage extends StatefulWidget {
  const HadithManagementPage({super.key});

  @override
  State<HadithManagementPage> createState() => _HadithManagementPageState();
}

class _HadithManagementPageState extends State<HadithManagementPage> {
  final HadithRepository _hadithRepository = HadithRepository();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Hadith Management'),
        centerTitle: true,
      ),
      body: StreamBuilder<List<Hadith>>(
        stream: _hadithRepository.getHadiths(),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
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
          }
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          final hadiths = snapshot.data ?? [];
          if (hadiths.isEmpty) {
            return Center(
              child: Card(
                margin: const EdgeInsets.all(24),
                child: Padding(
                  padding: const EdgeInsets.all(32),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.format_quote_rounded,
                          size: 48, color: AppColors.grey),
                      const SizedBox(height: 16),
                      Text(
                        'No hadith yet',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: AppColors.colorPrimary,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Tap + to add your first hadith.',
                        style: TextStyle(color: AppColors.subText),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }
          return ListView.builder(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 100),
            itemCount: hadiths.length,
            itemBuilder: (context, index) {
              final hadith = hadiths[index];
              return Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Card(
                  clipBehavior: Clip.antiAlias,
                  child: ListTile(
                    contentPadding: const EdgeInsets.all(16),
                    title: Text(
                      hadith.text,
                      maxLines: 3,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontWeight: FontWeight.w600,
                        color: AppColors.text,
                        height: 1.4,
                      ),
                    ),
                    subtitle: Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: Text(
                        hadith.narrator,
                        style: TextStyle(
                          fontSize: 13,
                          color: AppColors.subText,
                        ),
                      ),
                    ),
                    isThreeLine: true,
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: Icon(Icons.edit_outlined,
                              color: AppColors.colorPrimary),
                          onPressed: () =>
                              _showAddEditHadithDialog(hadith: hadith),
                        ),
                        IconButton(
                          icon: Icon(
                            Icons.delete_outline_rounded,
                            color: AppColors.failureRed,
                          ),
                          onPressed: () => _deleteHadith(hadith.id),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showAddEditHadithDialog(),
        backgroundColor: AppColors.colorPrimary,
        foregroundColor: AppColors.whiteSolid,
        icon: const Icon(Icons.add_rounded),
        label: Text(
          'Add Hadith',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  void _showAddEditHadithDialog({Hadith? hadith}) {
    final textController = TextEditingController(text: hadith?.text ?? '');
    final narratorController = TextEditingController(
      text: hadith?.narrator ?? '',
    );

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
          ),
          title: Text(
            hadith == null ? 'Add Hadith' : 'Edit Hadith',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              color: AppColors.colorPrimary,
            ),
          ),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: textController,
                  decoration: const InputDecoration(labelText: 'Hadith Text'),
                  maxLines: 5,
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: narratorController,
                  decoration: const InputDecoration(labelText: 'Narrator'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                final newHadith = Hadith(
                  id: hadith?.id ??
                      DateTime.now().millisecondsSinceEpoch.toString(),
                  text: textController.text,
                  narrator: narratorController.text,
                );
                if (hadith == null) {
                  _addHadith(newHadith);
                } else {
                  _editHadith(newHadith);
                }
                Navigator.of(context).pop();
              },
              child: Text(hadith == null ? 'Add' : 'Update'),
            ),
          ],
        );
      },
    );
  }

  void _addHadith(Hadith hadith) {
    _hadithRepository.addHadith(hadith);
  }

  void _editHadith(Hadith hadith) {
    _hadithRepository.updateHadith(hadith);
  }

  void _deleteHadith(String id) {
    _hadithRepository.deleteHadith(id);
  }
}
