package com.example.handoverbackend.service.board;

import com.example.handoverbackend.domain.board.Board;
import com.example.handoverbackend.domain.board.Favorite;
import com.example.handoverbackend.domain.category.Category;
import com.example.handoverbackend.domain.member.Member;
import com.example.handoverbackend.dto.board.BoardCreateRequestDto;
import com.example.handoverbackend.dto.board.BoardFindAllWithPagingResponseDto;
import com.example.handoverbackend.dto.board.BoardFindResponseDto;
import com.example.handoverbackend.dto.board.BoardUpdateRequestDto;
import com.example.handoverbackend.factory.BoardMaker;
import com.example.handoverbackend.repository.board.BoardRepository;
import com.example.handoverbackend.repository.board.FavoriteRepository;
import com.example.handoverbackend.repository.category.CategoryRepository;
import com.example.handoverbackend.service.file.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.handoverbackend.domain.board.Favorite.*;
import static com.example.handoverbackend.domain.category.Category.*;
import static com.example.handoverbackend.factory.ImageMaker.createImage;
import static com.example.handoverbackend.factory.ImageMaker.createImages;
import static com.example.handoverbackend.factory.MemberMaker.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    @InjectMocks
    BoardService boardService;
    @Mock
    BoardRepository boardRepository;
    @Mock
    FavoriteRepository favoriteRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    FileService fileService;

    @BeforeEach
    public void beforeEach() {
        Category category = createCategory("category");
    }

    @Test
    @DisplayName("게시판 생성")
    void createBoard() {
        //given
        BoardCreateRequestDto req = new BoardCreateRequestDto("title", "content", createImages());
        Member member = createMember();
        Category category = createCategory("category");
        given(categoryRepository.findById(0L)).willReturn(Optional.of(category));

        //when
        boardService.createBoard(req, member, 0L);

        //then
        verify(boardRepository).save(any());
    }

    @Test
    @DisplayName("게시판 목록 조회")
    void findAllBoardsWithCategory() {
        //given
        Integer page = 1;
        Long categoryId = 1l;
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
        List<Board> boards = List.of(BoardMaker.createBoard());
        Page<Board> boardsWithPaging = new PageImpl<>(boards);
        given(boardRepository.findAllByCategoryId(categoryId, pageRequest)).willReturn(boardsWithPaging);

        //when
        BoardFindAllWithPagingResponseDto result = boardService.findAllBoardsWithCategory(page, categoryId);

        //then
        assertThat(result.getBoards().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시판 전체 조회")
    void findAllBoards() {
        //given
        Integer page = 1;
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
        List<Board> boards = List.of(BoardMaker.createBoard());
        Page<Board> boardsWithPaging = new PageImpl<>(boards);
        given(boardRepository.findAll(pageRequest)).willReturn(boardsWithPaging);

        //when
        BoardFindAllWithPagingResponseDto result = boardService.findAllBoards(page);

        //then
        assertThat(result.getBoards().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시판 단건 조회")
    void findBoard() {
        //given
        Long id = 1L;
        Board board = BoardMaker.createBoard();
        given(boardRepository.findById(id)).willReturn(Optional.of(board));

        //when
        BoardFindResponseDto result = boardService.findBoard(id);

        //then
        assertThat(result.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("게시판 수정")
    void editBoard() {
        //given
        Long id = 1l;
        BoardUpdateRequestDto req = new BoardUpdateRequestDto("title", "content", createImages(), List.of());
        Member member = createMember();
        Board board = Board.createBoard("t", "c", member, null, List.of(createImage()));
        given(boardRepository.findById(id)).willReturn(Optional.of(board));

        //when
        boardService.editBoard(id, req, member);

        //then
        assertThat(board.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("게시판 삭제")
    void deleteBoard() {
        //given
        Long id = 1l;
        Board board = BoardMaker.createBoard();
        Member member = board.getMember();
        given(boardRepository.findById(id)).willReturn(Optional.of(board));

        //when
        boardService.deleteBoard(id, member);

        //then
        verify(boardRepository).delete(any());
    }

    @Test
    @DisplayName("즐겨찾기 추가")
    void updateFavoriteBoard() {
        //given
        Long id = 1L;
        Member member = createMember();
        Board board = BoardMaker.createBoard();

        given(boardRepository.findById(id)).willReturn(Optional.of(board));
        given(favoriteRepository.findByBoardAndMember(board, member)).willReturn(Optional.empty());

        //when
        String result = boardService.updateFavoriteBoard(id, member);


        // then
        assertThat(result).isEqualTo("즐겨찾기를 하었습니다.");
        verify(favoriteRepository).save(any());
    }


    @Test
    @DisplayName("즐겨찾기 해제")
    void deleteFavoriteBoard() {
        //given
        Long id = 1L;
        Member member = createMember();
        Board board = BoardMaker.createBoard();
        Favorite favorite = createFavorite(board, member);

        given(boardRepository.findById(id)).willReturn(Optional.of(board));
        given(favoriteRepository.findByBoardAndMember(board, member)).willReturn(Optional.of(favorite));

        //when
        String result = boardService.updateFavoriteBoard(id, member);

        // then
        assertThat(result).isEqualTo("즐겨찾기가 해제되었습니다.");
        verify(favoriteRepository).delete(any());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회")
    void findFavoriteBoards() {
        //given
        Integer page = 0;
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
        Member member = createMember();
        List<Favorite> favorites = new ArrayList<>();
        favorites.add(new Favorite(1L, BoardMaker.createBoard(), member));
        Page<Favorite> favoritesWithPaging = new PageImpl<>(favorites);
        given(favoriteRepository.findAllByMember(member, pageRequest)).willReturn(favoritesWithPaging);

        //when
        BoardFindAllWithPagingResponseDto result = boardService.findFavoriteBoards(page, member);

        //then
        assertThat(result.getBoards().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("게시판 검색 조회")
    void searchBoard() {
        //given
        String keyword = "title";
        Integer page = 1;

        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("id").descending());
        List<Board> boards = new ArrayList<>();
        boards.add(BoardMaker.createBoard());
        Page<Board> boardsWithPaging = new PageImpl<>(boards);
        given(boardRepository.findAllByTitleContaining(keyword, pageRequest)).willReturn(boardsWithPaging);

        //when
        BoardFindAllWithPagingResponseDto result = boardService.searchBoard(keyword, page);

        //then
        assertThat(result.getBoards().size()).isEqualTo(1);
    }
}