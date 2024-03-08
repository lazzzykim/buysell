package com.teamsparta.buysell.domain.post.service

import com.teamsparta.buysell.domain.common.dto.MessageResponse
import com.teamsparta.buysell.domain.exception.ModelNotFoundException
import com.teamsparta.buysell.domain.member.model.Member
import com.teamsparta.buysell.domain.member.repository.MemberRepository
import com.teamsparta.buysell.domain.post.dto.request.CreatePostRequest
import com.teamsparta.buysell.domain.post.dto.request.UpdatePostRequest
import com.teamsparta.buysell.domain.post.dto.response.PostListResponse
import com.teamsparta.buysell.domain.post.dto.response.PostResponse
import com.teamsparta.buysell.domain.post.model.Category
import com.teamsparta.buysell.domain.post.model.Like
import com.teamsparta.buysell.domain.post.model.Post
import com.teamsparta.buysell.domain.post.model.toResponse
import com.teamsparta.buysell.domain.post.repository.LikeRepository
import com.teamsparta.buysell.domain.post.repository.PostRepository
import com.teamsparta.buysell.infra.security.UserPrincipal
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
    private val likeRepository: LikeRepository
) : PostService {
    @Transactional
    override fun createPost(request: CreatePostRequest, principal: UserPrincipal): PostResponse {
        val member = getMember(principal)

        return postRepository.save(
            Post(
                title = request.title,
                content = request.content,
                view = 0,
                price = request.price,
                member = member,
                category = request.category,
            )
        ).toResponse()
    }

    @Transactional
    override fun updatePost(
        postId: Int,
        request: UpdatePostRequest,
        principal: UserPrincipal
    ): PostResponse {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw ModelNotFoundException("post", postId)

        post.checkPermission(principal)

        post.title = request.title
        post.content = request.content
        post.price = request.price

        return postRepository.save(post).toResponse()
    }

    override fun getPosts(): List<PostResponse> {
        return postRepository.findAll().map { it.toResponse() }

    }

    override fun getPostById(postId: Int): PostResponse {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw ModelNotFoundException("post", postId)
        return post.toResponse()
    }

//    @Transactional
    override fun deletePost(postId: Int, principal: UserPrincipal) {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw ModelNotFoundException("post", postId)

        post.checkPermission(principal)

        postRepository.delete(post)
//        post.softDelete()
    }

    //게시글을 조회할 때 Pagination을 적용한 메서드
    //카테고리가 없을 경우 기존과 동일하게 동작
    //카테고리가 있을 경우 해당 카테고리 관련 게시글만 조회
    override fun getPostsWithPagination(
        category: Category?,
        pageable: Pageable
    ): Page<PostListResponse> {
        return postRepository
            .getPostsWithPagination(category, pageable)
    }

    //키워드 검색 메서드
    override fun searchByKeyword(
        keyword: String,
        pageable: Pageable
    ): Page<PostListResponse> {
        return postRepository
            .searchByKeyword(keyword, pageable)
    }

    override fun addLikes(
        postId: Int,
        userPrincipal: UserPrincipal
    ) : MessageResponse {
        val post = getPost(postId)

        //작성자는 자기 게시글에 찜 버튼을 누를 수 없다
        Like.checkPermission(post, userPrincipal)

        likeRepository.existsByPostIdAndMemberId(postId,userPrincipal.id)
            .let { if(it) throw IllegalStateException("이미 찜을 등록하셨습니다.") }

        val member = getMember(userPrincipal)

        Like.makeEntity(post = post, member = member)
            .let { likeRepository.save(it) }

        return MessageResponse("찜 목록에 등록하였습니다.")
    }

    @Transactional
    override fun cancelLikes(
        postId: Int,
        userPrincipal: UserPrincipal
    ): MessageResponse {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw ModelNotFoundException("post", postId)

        //작성자는 자기 게시글에 찜 버튼을 누를 수 없다
        Like.checkPermission(post, userPrincipal)

        if (likeRepository.existsByPostIdAndMemberId(postId,userPrincipal.id)) {
            likeRepository.deleteByPostIdAndMemberId(postId, userPrincipal.id)
            return MessageResponse("찜이 취소되었습니다.")
        }
        else{
            return MessageResponse("잘못된 동작입니다.")
        }

//        val likeEntity = likeRepository.findByPostIdAndMemberId(postId, userPrincipal.id)
//            ?: throw ModelNotFoundException("like", null)
//
//        likeRepository.delete(likeEntity)


    }

    private fun getPost(postId: Int): Post{
        return postRepository.findByIdOrNull(postId)
            ?: throw ModelNotFoundException("post", postId)
    }

    private fun getMember(userPrincipal: UserPrincipal): Member{
        return memberRepository.findByIdOrNull(userPrincipal.id)
            ?: throw ModelNotFoundException("model", userPrincipal.id)
    }
}