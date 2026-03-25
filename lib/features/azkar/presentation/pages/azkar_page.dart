import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_bloc.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_event.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_state.dart';
import 'package:islami_admin/features/home/presentation/widgets/custom_drawer.dart';

class AzkarPage extends StatefulWidget {
  const AzkarPage({super.key});

  @override
  State<AzkarPage> createState() => _AzkarPageState();
}

class _AzkarPageState extends State<AzkarPage> {
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    context.read<AzkarBloc>().add(FetchAzkar());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Azkar Management'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.cloud_download_outlined),
            tooltip: 'Refresh',
            onPressed: () => context.read<AzkarBloc>().add(FetchAzkar()),
          ),
          const SizedBox(width: 8),
        ],
      ),
      drawer: const CustomDrawer(),
      body: BlocConsumer<AzkarBloc, AzkarState>(
        listener: (context, state) {
          if (state is AzkarLoaded) {
            _controller.text = state.content;
          } else if (state is AzkarError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.message),
                backgroundColor: Colors.redAccent,
              ),
            );
          } else if (state is AzkarSaved) {
            _showSuccessSnackBar();
          }
        },
        builder: (context, state) {
          return Column(
            children: [
              _buildEditorHeader(state is AzkarLoading),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
                  child: Card(
                    clipBehavior: Clip.antiAlias,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: state is AzkarLoading
                        ? const Center(child: CircularProgressIndicator())
                        : _buildJsonEditor(),
                  ),
                ),
              ),
            ],
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          context.read<AzkarBloc>().add(SaveAzkarEvent(_controller.text));
        },
        backgroundColor: AppColors.colorPrimary,
        foregroundColor: AppColors.whiteSolid,
        icon: const Icon(Icons.save_rounded),
        label: Text(
          'Save Changes',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _buildEditorHeader(bool isLoading) {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Row(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Azkar JSON Editor',
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: AppColors.colorPrimary,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Edit the raw JSON data for azkar.json file.',
                style: TextStyle(
                  fontSize: 14,
                  color: AppColors.subText,
                ),
              ),
            ],
          ),
          const Spacer(),
          if (isLoading)
            const Text('Syncing...')
          else
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: AppColors.success.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(
                    Icons.check_circle_rounded,
                    color: AppColors.success,
                    size: 14,
                  ),
                  const SizedBox(width: 6),
                  Text(
                    'Live from Storage',
                    style: TextStyle(
                      fontSize: 12,
                      color: AppColors.success,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildJsonEditor() {
    return TextField(
      controller: _controller,
      maxLines: null,
      style: GoogleFonts.firaCode(
        fontSize: 14,
        color: AppColors.text,
        height: 1.5,
      ),
      decoration: InputDecoration(
        hintText: 'Enter JSON content...',
        border: InputBorder.none,
        enabledBorder: InputBorder.none,
        focusedBorder: InputBorder.none,
        contentPadding: const EdgeInsets.all(24),
        fillColor: Colors.transparent,
      ),
    );
  }

  void _showSuccessSnackBar() {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            Icon(Icons.check_circle_rounded, color: AppColors.whiteSolid),
            const SizedBox(width: 12),
            Text(
              'Azkar saved and updated successfully!',
              style: TextStyle(color: AppColors.whiteSolid),
            ),
          ],
        ),
        backgroundColor: AppColors.success,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        margin: const EdgeInsets.all(24),
      ),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
